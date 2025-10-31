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

	// v1 group
	api := app.Group("/api/v1")

	public := api.Group("/")
	// auth := api.Group("/", middleware.JWTProtected())

	users := public.Group("/users")
	{
		users.Post("/", userHandler.CreateUser)
		users.Post("/login", userHandler.TryLogin)
	}
	
	posts := public.Group("/posts")
	{

		// 
		posts.Get("/preview", postHandler.GetPostsSimple)
		posts.Get("/:post_id", postHandler.GetPost)
		posts.Get("/:post_id/interaction", postHandler.GetUserInteraction)

		posts.Get("/", postHandler.GetPosts)


		posts.Post("/", postHandler.CreatePost)
		posts.Post("/:post_id/upload-image", postHandler.UploadImageHandler)
		posts.Post("/:post_id/interaction", postHandler.PostInteraction)

		
	}

	prefeitura := public.Group("/prefeitura")
	{
		prefeitura.Get("/users", userHandler.GetUsers)
	}


}
