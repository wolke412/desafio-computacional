package entities

import (
	"time"
)

type E_Post struct {

	ID         int64  `json:"id_post"`
	Title string `json:"title"`
	Body string `json:"body,omitempty"`

	Type string `json:"post_type"`

	UserID int `json:"id_user,omitempty"`
	
	Lat float64 `json:"latitude"`
	Lon float64 `json:"longitude"`

	Images []string `json:"post_images,omitempty"`

	UpvoteCount 	 	int							`json:"upvote_count,omitempty"`
	DownvoteCount 	 	int							`json:"downvote_count,omitempty"`
	
	CreatedAt  time.Time `json:"created_at"`
}
