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

	userService := services.NewUserService(userRepo)

	userHandler := handlers.NewUserHandler(userService)

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

}
