package db

import (
	"database/sql"
	"fmt"
	"log"
	"os"

	_ "github.com/lib/pq"
)

// uso pra testar fora do docker
const (
	DB_HOST     = "localhost"
	DB_PORT     = "5433"
	DB_USER     = "trabalho"
	DB_PASSWORD = "trabalho"
	DB_NAME     = "trabalho"
)

type Database struct {
	conn *sql.DB
}

var instance *Database

// ------------------------------------------------------------
// initializes the database connection trying
// to using env vars.
// ------------------------------------------------------------
func Connect() error {
	host := os.Getenv("DB_HOST")
	if host == "" {
		host = DB_HOST
	}

	port := os.Getenv("DB_PORT")
	if port == "" {
		port = DB_PORT
	}

	user := os.Getenv("DB_USER")
	if user == "" {
		user = DB_USER
	}

	password := os.Getenv("DB_PASSWORD")
	if password == "" {
		password = DB_PASSWORD
	}

	dbname := os.Getenv("DB_NAME")
	if dbname == "" {
		dbname = DB_NAME
	}

	psqlInfo := fmt.Sprintf(
		"host=%s port=%s user=%s password=%s dbname=%s sslmode=disable",
		host, port, user, password, dbname,
	)

	conn, err := sql.Open("postgres", psqlInfo)
	if err != nil {
		return fmt.Errorf("error opening DB: %w", err)
	}

	if err = conn.Ping(); err != nil {
		return fmt.Errorf("error pinging DB: %w", err)
	}

	log.Printf("[DB] Connected to PostgreSQL on %s:%s db=%s user=%s", host, port, dbname, user)

	instance = &Database{conn: conn}
	return nil
}

func Get() *sql.DB {
	if instance == nil || instance.conn == nil {
		log.Fatal("[DB] Connect() must be called before Get().")
	}
	return instance.conn
}

func Close() {
	if instance != nil && instance.conn != nil {
		if err := instance.conn.Close(); err != nil {
			log.Printf("[DB] Error closing DB: %v\n", err)
		} else {
			log.Println("[DB] Connection closed.")
		}
	}
}
