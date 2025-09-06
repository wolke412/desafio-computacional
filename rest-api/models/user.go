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
	ID         int64    

	BairroID int64 

	Name       string   

	Email      string  
	CPF string  
	Password   string 
	
	Type UserType
	CreatedAt  time.Time
}
