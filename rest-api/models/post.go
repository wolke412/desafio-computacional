package models

import "time"

type Post struct  {
	ID         int64  `json:"id_post"`
	Title string `json:"title"`
	Body string `json:"body"`

	Type string `json:"post_type"`

	UserID int `json:"id_user"`
	
	Lat float64 `json:"latitude"`
	Lon float64 `json:"longitude"`
	
	CreatedAt  time.Time `json:"created_at"`
}

type PostAttachment struct  {
	ID         int64  `json:"id_attachment"`

	AttachmentName string `json:"attachment_name"`
	Path string `json:"path"`

	Type string `json:"type"`
	
	CreatedAt  time.Time `json:"created_at"`
}
