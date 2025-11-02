package repositories

import (
	"context"
	"database/sql"
	"errors"
	"fmt"
	"rest-api/models"
)

type UserRepository struct {
	DB *sql.DB
}

func NewUserRepository(db *sql.DB) *UserRepository {
	return &UserRepository{DB: db}
}

// Cria um usuário
func (r *UserRepository) Create(ctx context.Context, user *models.User) error {
	fmt.Printf("\n USER: \n %+v\n", *user)
	query := `
		INSERT INTO users 
			( user_name, email, password_hash, cpf, id_bairro, type) 
		VALUES 
			($1, $2, $3, $4, $5, $6) 
		RETURNING 
			id_user
	`

	return r.DB.QueryRowContext(ctx, query, user.Name, user.Email, user.Password, user.CPF, user.BairroID, user.Type).Scan(&user.ID)
}

// ------------------------------------------------------------
//
//	Delete usuário pelo ID
func (r *UserRepository) Delete(ctx context.Context, id int) error {
	query := `DELETE FROM users WHERE id_user = $1`
	_, err := r.DB.ExecContext(ctx, query, id)
	return err
}

// ------------------------------------------------------------
//
//	Busca e rotrna um usuário pelo ID
func (r *UserRepository) FindById(ctx context.Context, id int) (*models.User, error) {
	query := `SELECT * FROM users WHERE id_user = $1`

	var user models.User
	err := r.DB.QueryRowContext(ctx, query, id).Scan(
		&user.ID,
		&user.Name,
		&user.Email,
		&user.Password,
		&user.CPF,
		&user.BairroID,
		&user.Type,
		&user.CreatedAt,
	)
	if err != nil {
		if err == sql.ErrNoRows {
			return nil, errors.New("user does not exist")
		}
		return nil, err
	}

	return &user, nil
}

// ------------------------------------------------------------
//
//	Busca e rotrna um usuário pelo EMAILj
func (r *UserRepository) FindByEmail(ctx context.Context, email string) (*models.User, error) {
	query := `SELECT * FROM users WHERE email = $1`

	var user models.User
	err := r.DB.QueryRowContext(ctx, query, email).Scan(
		&user.ID,
		&user.Name,
		&user.Email,
		&user.Password,
		&user.CPF,
		&user.BairroID,
		&user.Type,
		&user.CreatedAt,
	)
	if err != nil {
		if err == sql.ErrNoRows {
			return nil, errors.New("user does not exist")
		}
		return nil, err
	}

	return &user, nil
}

func (r *UserRepository) FindByEmailAndPassword(ctx context.Context, email string, password string) (*models.User, error) {

	query := `SELECT 
		id_user, user_name, email, password_hash, cpf, id_bairro, type,
		created_at
	FROM users WHERE email = $1 AND password_hash = $2`

	var user models.User
	err := r.DB.QueryRowContext(ctx, query, email, password).Scan(
		&user.ID,
		&user.Name,
		&user.Email,
		&user.Password,
		&user.CPF,
		&user.BairroID,
		&user.Type,
		&user.CreatedAt,
	)
	if err != nil {
		if err == sql.ErrNoRows {
			return nil, errors.New("user does not exist")
		}
		return nil, err
	}

	return &user, nil
}

func (r *UserRepository) FindAll(ctx context.Context) ([]models.User, error) {
	rows, err := r.DB.Query("SELECT * FROM users")
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var users []models.User = make([]models.User, 0)

	for rows.Next() {
		var u models.User

		err := rows.Scan(
			&u.ID,
			&u.Name,
			&u.Email,
			&u.Password,
			&u.CPF,
			&u.BairroID,
			&u.Type,
			&u.CreatedAt,
		)

		if err != nil {
			return nil, err
		}
		users = append(users, u)
	}
	return users, nil
}
