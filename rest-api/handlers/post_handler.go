package handlers

import (
	"rest-api/models"
	"rest-api/services"

	"github.com/gofiber/fiber/v2"
)

type PostHandler struct {
	Service *services.PostService
}

func NewPostHandler(service *services.PostService) *PostHandler {
	return &PostHandler{Service: service}
}

func (h *PostHandler) CreatePost(c *fiber.Ctx) error {
	var post models.Post 

	if err := c.BodyParser(&post); err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"error": "Invalid request body",
		})
	}
	err := h.Service.Create(c.Context(), &post)

	if err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{
			"error": err.Error(),
		})
	}

	return c.Status(fiber.StatusCreated).JSON(post)
}

func (h *PostHandler) GetPosts(c *fiber.Ctx) error {
	posts, err := h.Service.GetAllPosts(c.Context())
	if err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{"error": "failed to fetch posts"})
	}
	return c.JSON(posts)
}
