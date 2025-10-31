package main

import (
	"log"
	"os"

	"github.com/gofiber/fiber/v2"
	"github.com/gofiber/fiber/v2/middleware/cors"
	"github.com/gofiber/fiber/v2/middleware/logger"

	"rest-api/db" // adjust if your module path differs
	"rest-api/routes"
)

func main() {
	err := db.Connect()

	if err != nil {
		log.Fatalf("failed to connect to db: %v", err)
	}

	defer db.Close()

	app := fiber.New(fiber.Config{

		StrictRouting: true,

		// erros não tratados no handler cairão aqui
		ErrorHandler: func(c *fiber.Ctx, err error) error {
			// default 500 status code
			code := fiber.StatusInternalServerError
			msg := "internal server error"

			if e, ok := err.(*fiber.Error); ok {
				code = e.Code
				msg = e.Message
			}

			return c.Status(code).JSON(fiber.Map{
				"error": msg,
			})
		},
	})

	app.Use(cors.New(cors.Config{
		AllowOrigins:     "http://localhost:3000",
		AllowMethods:     "GET,POST,PUT,PATCH,DELETE,OPTIONS",
		AllowHeaders:     "Origin, Content-Type, Accept",
		AllowCredentials: true,
	}))

	app.Use(logger.New())

	// serve public images
	app.Static("/uploads/images", "./uploads/images")

	routes.SetupRoutes(app, db.Get())

	// server to run the local client
	port := os.Getenv("PORT")
	if port == "" {
		port = "44444"
	}
	log.Println("API Rodando em: http://0.0.0.0:" + port)

	log.Fatal(app.Listen(":" + port))

}
