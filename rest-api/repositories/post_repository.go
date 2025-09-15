package repositories

import (
	"context"
	"database/sql"
	"errors"
	"fmt"
	"rest-api/models"
)

type PostRepository struct {
	DB *sql.DB
}

func NewPostRepository(db *sql.DB) *PostRepository {
	return &PostRepository{DB: db}
}

// Cria um usuário
func (r *PostRepository) Create(ctx context.Context, post *models.Post) error {
	fmt.Printf("\n USER: \n %+v\n", *post)
	query := `
		INSERT INTO posts 
			( title, body, id_bairro, id_user ) 
		VALUES 
			($1, $2, $3, $4 ) 
		RETURNING 
			id_post
	`

	return r.DB.QueryRowContext(ctx, query, 
		post.Title,
		post.Body,
		post.BairroID,
		post.UserID, 
	).Scan(&post.ID)

}

// ------------------------------------------------------------
//
func (r *PostRepository) Delete(ctx context.Context, id int) error {
	query := `DELETE FROM posts WHERE id_post = $1`
	_, err := r.DB.ExecContext(ctx, query, id)
	return err
}

// ------------------------------------------------------------
//
func (r *PostRepository) FindById(ctx context.Context, id int) (*models.Post, error) {
	query := `SELECT * FROM posts WHERE id_post = $1`

	var post models.Post
	err := r.DB.QueryRowContext(ctx, query, id).Scan(
		&post.ID,
		&post.Title,
		&post.Body,
		&post.BairroID,
		&post.UserID,
		&post.CreatedAt,
	)
	if err != nil {
		if err == sql.ErrNoRows {
			return nil, errors.New("post does not exist")
		}
		return nil, err
	}

	return &post, nil
}

// ------------------------------------------------------------
//

func (r *PostRepository) FindByEmailAndPassword(ctx context.Context, email string, password string) (*models.Post, error) {

	query := `SELECT * FROM posts WHERE email = $1 AND password = $2`

	var post models.Post
	err := r.DB.QueryRowContext(ctx, query, email, password).Scan(
		&post.ID,
		&post.Title,
		&post.Body,
		&post.BairroID,
		&post.UserID,
		&post.CreatedAt,
	)
	if err != nil {
		if err == sql.ErrNoRows {
			return nil, errors.New("post does not exist")
		}
		return nil, err
	}

	return &post, nil
}

func (r *PostRepository) FindAll(ctx context.Context) ([]models.Post, error) {
	rows, err := r.DB.Query("SELECT * FROM posts")
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var posts []models.Post = make([]models.Post, 0)

	for rows.Next() {
		var u models.Post

		err := rows.Scan(
			&u.ID,
			&u.Title,
			&u.Body,
			&u.BairroID,
			&u.UserID,
			&u.CreatedAt,
		)

		if err != nil {
			return nil, err
		}
		posts = append(posts, u)
	}
	return posts, nil
}
