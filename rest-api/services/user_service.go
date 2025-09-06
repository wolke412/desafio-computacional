package services

import (
	"context"
	"crypto/aes"
	"crypto/cipher"
	"crypto/rand"
	"crypto/sha256"
	"encoding/base64"
	"encoding/hex"
	"errors"
	"fmt"
	"io"
	"os"
	"regexp"
	"rest-api/entities"
	"rest-api/models"
	"rest-api/repositories"
	"strconv"

	"github.com/gofiber/fiber/v2"
)

type UserService struct {
	UserRepo *repositories.UserRepository
}

func NewUserService(
	repo *repositories.UserRepository,
) *UserService {
	return &UserService{
		UserRepo: repo,
	}
}

func (s *UserService) GetAllUsers(ctx context.Context) ([]models.User, error) {
	return s.UserRepo.FindAll(ctx)
}

func (s *UserService) Create(ctx context.Context, user *entities.E_UserSignUp) error {

	if user.Type != models.CIDADAO {
		return fiber.NewError(fiber.StatusBadRequest, "somente cadastros de tipo cidadão são permitidos.")
	}

	// ------------------------------------------------------------
	// Idealmente seria interessante validar o CPF junto do nome
	if !isValidCPF(user.CPF) {
		return fiber.NewError(fiber.StatusBadRequest, "CPF inválido.")
	}

	if len(user.Name) < 5 {
		return fiber.NewError(fiber.StatusBadRequest, "insira seu nome completo")
	}
	// ------------------------------------------------------------
	if user.Email == "" || user.Password == "" {
		return fiber.NewError(fiber.StatusBadRequest, "email e senha são obrigatórios")
	}

	exists, _ := s.UserRepo.FindByEmail(ctx, user.Email)
	if exists != nil {
		return fiber.NewError(fiber.StatusBadRequest, "este email já está em uso")
	}

	// ------------------------------------------------------------
	// Se estiver se perguntando pq a hash do CPF precisa de
	// validação de erro e a da senha não
	// É pq a do CPF precisa ser reversível;
	// E pra tornar ela reversível tem que usar uma "chave" conhecida;
	// e pra ela ser conhecida tem que estar no código...
	// mas pra ela estar no código precisa estar segura,
	// no nossos caso colocamos no ambiente; Mas idealmente estaria em um VAULT
	// tipo o da amazon.
	// Pra pegar essa chave precisaria se comunicar com outro processo, e isso
	// pode gerar um erro ;)
	hashpass := hashPassword(user.Password)
	hashcpf, err := encryptCPF(user.CPF)

	if err != nil {
		return err
	}

	user.Password = hashpass
	user.CPF = hashcpf

	// converte entidade pra model por uma questão de COESÃO
	// na prática não muda nada
	return s.UserRepo.Create(ctx, user.ToUserModel())
}

func (s *UserService) Delete(ctx context.Context, user_id int) error {
	return s.UserRepo.Delete(ctx, user_id)
}

func (s *UserService) TryLogin(ctx context.Context, email string, password string) (*models.User, error) {
	hash := hashPassword(password)
	return s.UserRepo.FindByEmailAndPassword(ctx, email, hash)
}

// ------------------------------------------------------------
// validação de CPF é algoritmo de luhn puro
// aqui tem validação de tamanho tb
func isValidCPF(cpf string) bool {
	fmt.Printf("Validating CPF\n")

	// Remover pontos e traços
	re := regexp.MustCompile(`[^\d]`)
	cpf = re.ReplaceAllString(cpf, "")

	// Verificar se o CPF tem 11 dígitos
	if len(cpf) != 11 {
		return false
	}

	// checa se todos os dígitos são iguais
	allSame := true
	for i := 1; i < 11; i++ {
		if cpf[i] != cpf[0] {
			allSame = false
			break
		}
	}

	if allSame {
		fmt.Printf("Validating CPF: all the same\n")
		return false
	}

	// Converter os primeiros 9 dígitos para inteiros
	numeros := make([]int, 11)
	for i := 0; i < 11; i++ {
		n, _ := strconv.Atoi(string(cpf[i]))
		numeros[i] = n
	}

	// Calcular o primeiro dígito verificador
	soma := 0
	for i := 0; i < 9; i++ {
		soma += numeros[i] * (10 - i)
	}

	resto := (soma * 10) % 11
	if resto == 10 {
		resto = 0
	}
	if resto != numeros[9] {
		return false
	}

	// Calcular o segundo dígito verificador
	soma = 0
	for i := 0; i < 10; i++ {
		soma += numeros[i] * (11 - i)
	}

	resto = (soma * 10) % 11
	if resto == 10 {
		resto = 0
	}
	if resto != numeros[10] {
		return false
	}

	// CPF válido
	return true
}

// +--
func hashPassword(pass string) string {
	hash := sha256.Sum256([]byte(pass))
	return hex.EncodeToString(hash[:])
}

// getKey pega a chave AES do .env (Base64)
func getKey() ([]byte, error) {
	keyB64 := os.Getenv("APP_AES_KEY_B64")
	if keyB64 == "" {
		return nil, errors.New("APP_AES_KEY_B64 not set")
	}
	return base64.StdEncoding.DecodeString(keyB64)
}

func encryptCPF(cpf string) (string, error) {
	key, err := getKey()
	if err != nil {
		return "", err
	}

	block, err := aes.NewCipher(key)
	if err != nil {
		return "", err
	}

	aesgcm, err := cipher.NewGCM(block)
	if err != nil {
		return "", err
	}

	nonce := make([]byte, aesgcm.NonceSize())
	if _, err := io.ReadFull(rand.Reader, nonce); err != nil {
		return "", err
	}

	ct := aesgcm.Seal(nil, nonce, []byte(cpf), nil)

	// Armazenar tudo junto: nonce|ciphertext
	result := append(nonce, ct...)
	return base64.StdEncoding.EncodeToString(result), nil
}

// DecryptCPF descriptografa CPF a partir de string base64
func decryptCPF(data string) (string, error) {
	key, err := getKey()
	if err != nil {
		return "", err
	}

	raw, err := base64.StdEncoding.DecodeString(data)
	if err != nil {
		return "", err
	}

	block, err := aes.NewCipher(key)
	if err != nil {
		return "", err
	}

	aesgcm, err := cipher.NewGCM(block)
	if err != nil {
		return "", err
	}

	nonceSize := aesgcm.NonceSize()
	if len(raw) < nonceSize {
		return "", errors.New("ciphertext too short")
	}

	nonce, ciphertext := raw[:nonceSize], raw[nonceSize:]
	plain, err := aesgcm.Open(nil, nonce, ciphertext, nil)
	if err != nil {
		return "", err
	}

	return string(plain), nil
}
