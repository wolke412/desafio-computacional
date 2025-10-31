package handlers

import (
	"fmt"
	"log/slog"
	"rest-api/entities"
	"rest-api/models"
	"rest-api/services"
	"strconv"

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

	fmt.Printf("\npost:\n %+v \n\n", post)

	err := h.Service.Create(c.Context(), &post)

	if err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{
			"error": err.Error(),
		})
	}

	return c.Status(fiber.StatusCreated).JSON(post)
}

func (h *PostHandler) UploadImageHandler(c *fiber.Ctx) error {

	post_id:= c.Params("post_id")
	if post_id == "" {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"error": "post inválido.",
		})
	}
	
	post_id_int, err := strconv.Atoi(post_id) 
	if err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"error": "post inválido.",
		})
	}

	file, err := c.FormFile("image")
	if err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"error": "failed to read image: " + err.Error(),
		})
	}

	err = h.Service.AttachFile(
			c.Context(),	
			post_id_int,	
			file,
	)

	if err != nil {
		return err
	}

	return c.SendStatus(fiber.StatusCreated)
}

func (h *PostHandler) PostInteraction(c *fiber.Ctx) error {

	post_id:= c.Params("post_id")
	if post_id == "" {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"error": "post inválido.",
		})
	}

	post_id_int, err := strconv.Atoi(post_id) 
	if err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"error": "post inválido.",
		})
	}

	var postInter models.PostInteraction

	if err := c.BodyParser(&postInter); err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"error": "Invalid request body",
		})
	}

	postInter.PostID = post_id_int

	fmt.Printf("\npost inter (%d):\n %+v \n\n", post_id_int, postInter)
	

	err = h.Service.PlaceInteraction(c.Context(), &postInter)

	if err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{
			"error": err.Error(),
		})
	}

	return c.Status(fiber.StatusCreated).JSON(postInter)
}


func (h *PostHandler) GetPost(c *fiber.Ctx) error {

	post_id:= c.Params("post_id")
	if post_id == "" {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"error": "post inválido.",
		})
	}

	post_id_int, err := strconv.Atoi(post_id) 
	if err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"error": "post inválido.",
		})
	}

	post, err := h.Service.GetPostById(c.Context(), post_id_int)

	if err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{"error": "failed to fetch post info: " + err.Error()})
	}

	interactions, err := h.Service.GetPostInteractions(c.Context(), post_id_int )
	if err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{"error": "failed to fetch post info: " + err.Error()})
	}

	e_post := entities.E_Post {
		ID: post.ID,
		Title: post.Title,
		Body: post.Body,
		Type: post.Type,
		UserID: post.UserID,
		Lat: post.Lat,
		Lon: post.Lon,
		Images: make([]string,0),
		UpvoteCount: interactions.Up,
		DownvoteCount: interactions.Down,
		CreatedAt: post.CreatedAt,

	}
	
	att, err := h.Service.PostRepo.GetPostAttachments(c.Context(), post_id_int)	

	if err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{"error": "failed to fetch post data: " + err.Error()})
	}
	
	for _, a := range att {
		e_post.Images = append(e_post.Images, a.Path)
	}
	
	return c.JSON(e_post)
}


func (h *PostHandler) GetPostsSimple(c *fiber.Ctx) error {
	
	slog.Info("GET POSTS SIMPLE")
	posts, err := h.Service.GetAllPosts(c.Context())

	if err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{"error": "failed to fetch posts: "+ err.Error()})
	}

	var ent []entities.E_Post = make([]entities.E_Post, 0, len(posts))
	
	for _, p := range posts {
		ent = append(ent, entities.E_Post{
			ID: p.ID,
			Title: p.Title,
			Type: p.Type,
			Lat: p.Lat,
			Lon: p.Lon,
		})	
	}

	return c.JSON(ent)
}

func (h *PostHandler) GetUserInteraction(c *fiber.Ctx) error {
	post_id:= c.Params("post_id")
	if post_id == "" {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"error": "post inválido.",
		})
	}

	post_id_int, err := strconv.Atoi(post_id) 
	if err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"error": "post inválido.",
		})
	}

	var body struct { UserId int `json:"id_user"` }

	if err := c.BodyParser(&body); err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"error": "Invalid request body",
		})
	}
	
	inter, err := h.Service.GetUserInteractionOnPost(c.Context(), body.UserId, post_id_int)

	if err != nil {
		slog.Error("Error Getting user interactions", "pid", post_id_int, "uid", body.UserId, "err", err.Error())
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{"error": "failed to fetch posts"})
	}

	return c.JSON(inter)
}


// ------------------------------------------------------------ 
func (h *PostHandler) GetPosts(c *fiber.Ctx) error {
	posts, err := h.Service.GetAllPosts(c.Context())
	if err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{"error": "failed to fetch posts"})
	}
	return c.JSON(posts)
}

