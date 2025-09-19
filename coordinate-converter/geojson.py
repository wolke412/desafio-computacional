import json

with open("in/export.geojson") as f:
    data = json.load(f)



features = data.get("features")

geometry = features[0].get("geometry", {})
if geometry.get("type") != "Polygon":
    print("Only Polygon geometries are supported right now")
    exit()

coords = geometry["coordinates"][0]  # outer ring

# output Kotlin code
print("object ParobePerimetro {")
print("    val coordinates: List<LatLon> = listOf(")
for lon, lat in coords:
    print(f"        LatLon({lat}, {lon}),")
print("    )")
print("}")
