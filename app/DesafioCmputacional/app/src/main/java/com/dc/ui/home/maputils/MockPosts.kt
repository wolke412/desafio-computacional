package com.dc.ui.home.maputils

import com.dc.entities.Post
import com.dc.entities.PostType

object MockPosts {
    val posts : List<Post> = listOf<Post>(

        Post(
            id_post=1,
            title="Alguma construção rolando aqui",
            body="Algum conteúdo que possa ser relevante",
            latitude = -29.62662577489841,
            longitude = -50.82894312606729,
            post_type = PostType.INFORMATIVO,
            post_images = listOf("https://www.feevale.br/Comum/midias/403dfa3f-3d14-4f63-9a32-40ca43a92a57/1920x600/Vestibular26-01_Medicina_Banners_Home_1920x600px.png")
        ),
        Post(
            id_post=1,
            title="Asfaltamento",
            body="é ",
//            points=listOf(LatLon(-29.66, -50.82894312606729)),
            latitude = -29.66,
            longitude = -50.82894312606729,
            post_type = PostType.REFORMA
        ),
//        Post(
//            id=1,
//            title="Mexendo nessa quadra aqui ó.",
//            body="...",
//            points=listOf(
//
//                LatLon(-29.626054002645606, -50.828131648480415),
//                LatLon(-29.625275257125487, -50.82819199818565),
//                LatLon(-29.625302070319787, -50.829074444986695),
//                LatLon(-29.62610762861259, -50.829039576268116),
//            ),
//            post_type = PostType.REFORMA
//        ),
    )


}