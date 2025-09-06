package entities

import "rest-api/models"

type E_UserSignUp struct {
	Name     string          `json:"user_name"`
	Type     models.UserType `json:"user_type"`
	BairroID int             `json:"id_bairro"`
	Email    string          `json:"email"`
	Password string          `json:"password"`
	CPF      string          `json:"cpf"`
}

func (E *E_UserSignUp) ToUserModel() *models.User {
	return &models.User{
		BairroID: int64(E.BairroID),
		Name:     E.Name,
		Email:    E.Email,
		Password: E.Password,
		CPF:      E.CPF,
		Type:     E.Type,
	}
}
