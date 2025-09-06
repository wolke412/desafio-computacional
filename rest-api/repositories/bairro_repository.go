package repositories

import (
	"context"
	"database/sql"
)

type BairroRepository struct {
	DB *sql.DB
}

func NewBairroRepository(db *sql.DB) *BairroRepository {
	return &BairroRepository{DB: db}
}

// func (r *BairroRepository) Create(ctx context.Context, user *models.Bairro) error {
// 	query := `INSERT INTO users ( email, password, entity_name) VALUES ($1, $2, $3) RETURNING id_user`
// 
// 	return r.DB.QueryRowContext(ctx, query, user.Email, user.Password, user.EntityName).Scan(&user.ID)
// }

// func (r *BairroRepository) Delete(ctx context.Context, id int) error {
// 	query := `DELETE FROM users WHERE id_user = $1`
// 	_, err := r.DB.ExecContext(ctx, query, id)
// 	return err
// }

func (r *BairroRepository) Exists(ctx context.Context, id int) (bool, error) {

	query := `SELECT COUNT(*) as c FROM bairros where id_bairo = $1`
	
	var count int

	err :=  r.DB.QueryRowContext(ctx, query, id).Scan(&count)

	return count > 0, err
}

// func (r *BairroRepository) FindById(ctx context.Context, id int) (*models.Bairro, error) {
// 	query := `SELECT id_user, email, password, entity_name FROM users WHERE id = $1`
// 
// 	var user models.Bairro
// 	err := r.DB.QueryRowContext(ctx, query, id).Scan(
// 		&user.ID,
// 		&user.Email,
// 		&user.Password,
// 		&user.EntityName,
// 	)
// 	if err != nil {
// 		if err == sql.ErrNoRows {
// 			return nil, errors.New("user does not exist")
// 		}
// 		return nil, err
// 	}
// 
// 	return &user, nil
// }
