package models

import "time"

type Post struct  {
	ID         int64  `json:"id_post"`
	Title string `json:"title"`
	Body string `json:"body"`
	BairroID int  `json:"id_bairro"`

	UserID int `json:"id_user"`

	CreatedAt  time.Time `json:"created_at"`

}
