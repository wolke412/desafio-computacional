package com.dc.ui.home.maputils

import com.dc.coordinates.LatLon
import com.dc.entities.Post
import com.dc.entities.PostType

object MockPosts {
    val posts : List<Post> = listOf<Post>(

        Post(
            id=1,
            title="Alguma construção rolando aqui",
            body="Algum conteúdo que possa ser relevante",
            points=listOf(LatLon(-29.62662577489841, -50.82894312606729)),
            post_type = PostType.INFORMATIVO
        ),
        Post(
            id=1,
            title="Asfaltamento",
            body="é ",
            points=listOf(LatLon(-29.66, -50.82894312606729)),
            post_type = PostType.REFORMA
        ),

        Post(
            id=1,
            title="Mexendo nessa quadra aqui ó.",
            body="...",
            points=listOf(

                LatLon(-29.626054002645606, -50.828131648480415),
                LatLon(-29.625275257125487, -50.82819199818565),
                LatLon(-29.625302070319787, -50.829074444986695),
                LatLon(-29.62610762861259, -50.829039576268116),
            ),
            post_type = PostType.REFORMA
        ),
    )

}