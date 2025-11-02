package models

import "time"



// -
// ------------------------------------------------------------ 
type UserType = string
const (
	CIDADAO 	UserType = "CI"
	PREFEITURA 	UserType = "PR"
)
// ------------------------------------------------------------ 
// -

type User struct {
	ID         int64   `json:"id"`

	BairroID int64 

	Name       string   `json:"name"`

	Email      string  `json:"email"`
	CPF string  
	Password   string 
	
	Type UserType
	CreatedAt  time.Time
}
