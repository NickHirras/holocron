plugins {
    kotlin("jvm") version "1.9.22"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

    // The Golden Lock - Matched exactly to the buf.gen.yaml generator versions
    implementation("com.google.protobuf:protobuf-kotlin:3.25.3")
    implementation("io.grpc:grpc-netty-shaded:1.62.2")
    implementation("io.grpc:grpc-protobuf:1.62.2")
    implementation("io.grpc:grpc-stub:1.62.2")
    implementation("io.grpc:grpc-kotlin-stub:1.4.1")

    // The legacy annotation required by the v25.3 generated code
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")

    val ktorVersion = "2.3.8"
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
}

application {
    mainClass.set("holocron.v1.ServerKt")
}

sourceSets {
    main {
        java { srcDir("src/main/gen") }
        kotlin { srcDir("src/main/gen") }
    }
}

