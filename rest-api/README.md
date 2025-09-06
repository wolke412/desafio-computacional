# Arquitetura Handler > Service > Repository > Model;

## Handler
É basicamente o controller, recebe as requisões HTTP e prepara o pacote para o *service*.

## Service
Services servem para garantir as regras de negócio do projeto. Por exemplo, é no service que as criptografias são
feitas, validações de permissão, etc.

## Repo 
Esse tem em toda arquitetur bem dizer, é só um wrapper pro banco de dados

## Model
Define a estrutura da entidade *no banco de dados*. 
É interessante que exista um modelo intermediário antes que o *service* retorne o dado bruto para
o *handler*. 
Para este projeto temos as *entities* também.

## Entity
Não faz parte da arquitetura original, mas é um jeito de organizar as ENTIDADES *QUE A API TRANSPORTA*.
Ex.: a *model* "User" contém senha, que pode precisar ser validada em algum cenário, mas em nenhum cenário
ela precisa ir à público, para isso, o *model* é mascarado por uma *entity*.