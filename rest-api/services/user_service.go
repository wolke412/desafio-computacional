package services

import (
	"context"
	"fmt"
	"io"
	"mime"
	"mime/multipart"
	"os"
	"rest-api/models"
	"rest-api/repositories"
	"time"

	"github.com/gofiber/fiber/v2"
)

type PostService struct {
	PostRepo *repositories.PostRepository
	UserRepo *repositories.UserRepository
}

func NewPostService(
	repo *repositories.PostRepository,
	urepo *repositories.UserRepository,
) *PostService {
	return &PostService{
		PostRepo: repo,
		UserRepo: urepo,
	}
}

func (s *PostService) GetAllPosts(ctx context.Context) ([]models.Post, error) {
	return s.PostRepo.FindAll(ctx)
}

func (s *PostService) GetPostById(ctx context.Context, post_id int) (*models.Post, error) {
	return s.PostRepo.FindById(ctx, post_id)
}

func (s *PostService) Create(ctx context.Context, post *models.Post) error {
	// ------------------------------------------------------------
	if post.Title == "" || post.Body == "" {
		return fiber.NewError(fiber.StatusBadRequest, "titulo e corpo não podem estar vazios")
	}
	
	u, _ := s.UserRepo.FindById(ctx, post.UserID)
	if u == nil {
		return fiber.NewError(fiber.StatusBadRequest, "usuário inválido")
	}

	// if post.BairroID == 0 {
	// 	return fiber.NewError(fiber.StatusBadRequest, "bairro inválido")
	// }

	return s.PostRepo.Create(ctx, post )
 }

func (s *PostService) Delete(ctx context.Context, post_id int) error {
	return s.PostRepo.Delete(ctx, post_id)
}

func (s *PostService) AttachFile(ctx context.Context, post_id int, f *multipart.FileHeader ) error {
	filename := fmt.Sprintf("%d-%d", time.Now().Unix(), post_id)
	ext := getFileExtension(f)	
	
	filename += ext
	err := saveImage(f, filename, "./uploads/images")
	
	s.PostRepo.AttachImage(ctx, post_id, filename, "/uploads/images/"+filename, repositories.JPG)

	return err
}


func getFileExtension(file *multipart.FileHeader) string {

	// map common MIME types to preferred extensions
	mimeMap := map[string]string{
		"image/jpeg": ".jpg",
		"image/png":  ".png",
		"image/gif":  ".gif",
		"image/webp": ".webp",
		"image/bmp":  ".bmp",
		"image/tiff": ".tiff",
	}

	if file.Header != nil {
		if ct := file.Header.Get("Content-Type"); ct != "" {
			if ext, ok := mimeMap[ct]; ok {
				return ext
			}
		}
		if ct := file.Header.Get("Content-Type"); ct != "" {
			if exts, _ := mime.ExtensionsByType(ct); len(exts) > 0 {
				return exts[0]
			}
		}
	}

	return ".bin"
}

func saveImage(file *multipart.FileHeader, name string, uploadDir string) error {

	if err := os.MkdirAll(uploadDir, os.ModePerm); err != nil {
		return  fmt.Errorf("failed to create upload directory: %w", err)
	}
	
	dstPath := uploadDir + "/" + name

	src, err := file.Open()
	if err != nil {
		return  fmt.Errorf("failed to open uploaded file: %w", err)
	}
	defer src.Close()

	dst, err := os.Create(dstPath)
	if err != nil {
		return fmt.Errorf("failed to create destination file: %w", err)
	}
	defer dst.Close()

	if _, err := io.Copy(dst, src); err != nil {
		return fmt.Errorf("failed to write file to disk: %w", err)
	}
	
	return  nil
}


