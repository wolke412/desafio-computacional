[out:json];

// converte uma busca nomeada em um id do osm
// salva resultado em uma variavek chamada .searchArea
{{geocodeArea:Parobé,Brazil}}->.searchArea;

// pega todos os pontos de fronteira administrativa
relation(area.searchArea)["boundary"="administrative"]["name"="Parobé"]["admin_level"="8"];

//(._;>;);
out geom;
