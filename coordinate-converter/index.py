import json

with open("in/parobe.json") as f:
    data = json.load(f)

relation = None
for element in data.get("elements", []):
    if element.get("type") == "relation":
        relation = element

if not relation:
    raise ValueError("No relation found in JSON!")

coords = []
# seen_refs = set()

for member in relation.get("members", []):
    if member.get("type") == "way" and member.get("role") == "outer":
        # ref = member.get("ref")

        # if ref in seen_refs:
        #     continue  

        # seen_refs.add(ref)

        for point in member.get("geometry", []):
            coords.append((point["lat"], point["lon"]))

# output Kotlin code
print("object ParobePerimetro {")
print("    val coordinates: List<LatLon> = listOf(")
for lat, lon in coords:
    print(f"        LatLon({lat}, {lon}),")
print("    )")
print("}")
