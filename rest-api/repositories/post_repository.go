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

// Cria um post
func (r *PostRepository) Create(ctx context.Context, post *models.Post) error {
	fmt.Printf("\n USER: \n %+v\n", *post)
	query := `
		INSERT INTO posts 
			( title, body, post_type, id_user, latitude, longitude ) 
		VALUES 
			($1, $2, $3, $4, $5, $6 ) 
		RETURNING 
			id_post
	`

	return r.DB.QueryRowContext(ctx, query, 
		post.Title,
		post.Body,
		post.Type,
		// post.BairroID,
		post.UserID, 
		post.Lat, 
		post.Lon, 
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
	query := `SELECT id_post, title, body, id_user, post_type, latitude, longitude, created_at FROM posts WHERE id_post = $1`

	var post models.Post
	err := r.DB.QueryRowContext(ctx, query, id).Scan(
		&post.ID,
		&post.Title,
		&post.Body,
		&post.UserID,
		&post.Type,
		&post.Lat,
		&post.Lon,
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

// ------
// 
func (r *PostRepository) FindAll(ctx context.Context) ([]models.Post, error) {
	rows, err := r.DB.Query("SELECT id_post, title, body, id_user, post_type, latitude, longitude, created_at FROM posts")

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
			&u.UserID,
			&u.Type,
			&u.Lat,
			&u.Lon,
			&u.CreatedAt,
		)

		if err != nil {
			return nil, err
		}
		posts = append(posts, u)
	}
	return posts, nil
}

func (r *PostRepository) GetPostAttachments( ctx context.Context, post_id int ) ([]models.PostAttachment, error) {

	var posts []models.PostAttachment = make([]models.PostAttachment, 0)
	
	rows, err  := r.DB.Query("SELECT attachment_name, path, type, created_at FROM post_attachments WHERE id_post = $1", post_id)
	if err != nil {
		return posts, err
	}
	defer rows.Close()


	for rows.Next() {
		var u models.PostAttachment

		err := rows.Scan(
			&u.AttachmentName,
			&u.Path,
			&u.Type,
			&u.CreatedAt,
		)

		if err != nil {
			return nil, err
		}
		posts = append(posts, u)
	}
	return posts, nil
}

// ------------------------------------------------------------
//
type PostAttachmentType = string
const (
	PNG PostAttachmentType = "PNG"
	JPG PostAttachmentType = "JPG"
	PDF PostAttachmentType = "PDF"
)

func (r *PostRepository) AttachImage(ctx context.Context, id_post int, name string, path string, typ PostAttachmentType ) error {

	query := `
		INSERT INTO post_attachments
			( id_post, attachment_name, path, type ) 
		VALUES 
			( $1, $2, $3, $4 ) 
	`

	return r.DB.QueryRowContext(ctx, query, 
		id_post, name, path, typ,
	).Scan()

}
