package routes

import (
	"database/sql"
	"rest-api/handlers"
	"rest-api/repositories"
	"rest-api/services"

	"github.com/gofiber/fiber/v2"
)

func SetupRoutes(app *fiber.App, db *sql.DB) {

	userRepo := repositories.NewUserRepository(db)
	postRepo := repositories.NewPostRepository(db)

	userService := services.NewUserService(userRepo)
	postService := services.NewPostService(postRepo, userRepo)

	userHandler := handlers.NewUserHandler(userService)
	postHandler := handlers.NewPostHandler(postService)

	// group
	api := app.Group("/api/v1")

	public := api.Group("/")
	// authed := api.Group("/", middleware.JWTProtected())

	prefeitura := public.Group("/prefeitura")
	{
		prefeitura.Get("/users", userHandler.GetUsers)
	}

	users := public.Group("/users")
	{
		users.Post("/", userHandler.CreateUser)
		// users.Get("/:userId", userHandler.GetUserById)
	}
	
	posts := public.Group("/posts")
	{
		posts.Post("/", postHandler.CreatePost)
		posts.Post("/:post_id/upload-image", postHandler.UploadImageHandler)
		posts.Get("/", postHandler.GetPosts)
		posts.Get("/preview", postHandler.GetPostsSimple)
		posts.Get("/:post_id", postHandler.GetPost)
		// users.Get("/:userId", userHandler.GetUserById)
	}

}
