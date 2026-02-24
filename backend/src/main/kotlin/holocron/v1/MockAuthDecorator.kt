package holocron.v1

import com.linecorp.armeria.common.HttpRequest
import com.linecorp.armeria.common.HttpResponse
import com.linecorp.armeria.server.DecoratingHttpServiceFunction
import com.linecorp.armeria.server.HttpService
import com.linecorp.armeria.server.ServiceRequestContext
import io.netty.util.AttributeKey

class MockAuthDecorator : DecoratingHttpServiceFunction {
    companion object {
        val USER_EMAIL_ATTR: AttributeKey<String> = AttributeKey.valueOf("USER_EMAIL")
    }

    override fun serve(delegate: HttpService, ctx: ServiceRequestContext, req: HttpRequest): HttpResponse {
        val email = req.headers().get("x-mock-user-id")
        if (email != null) {
            ctx.setAttr(USER_EMAIL_ATTR, email)
        }
        return delegate.serve(ctx, req)
    }
}
