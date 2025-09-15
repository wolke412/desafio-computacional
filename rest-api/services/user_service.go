package services

import (
	"context"
	"rest-api/models"
	"rest-api/repositories"

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

func (s *PostService) Create(ctx context.Context, post *models.Post) error {
	// ------------------------------------------------------------
	if post.Title == "" || post.Body == "" {
		return fiber.NewError(fiber.StatusBadRequest, "titulo e corpo não podem estar vazios")
	}
	
	u, _ := s.UserRepo.FindById(ctx, post.UserID)
	if u == nil {
		return fiber.NewError(fiber.StatusBadRequest, "usuário inválido")
	}

	if post.BairroID == 0 {
		return fiber.NewError(fiber.StatusBadRequest, "bairro inválido")
	}

	return s.PostRepo.Create(ctx, post )
 }

func (s *PostService) Delete(ctx context.Context, post_id int) error {
	return s.PostRepo.Delete(ctx, post_id)
}
