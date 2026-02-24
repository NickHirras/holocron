import { Component, inject, signal } from '@angular/core';
import { AuthService } from '../services/auth.service';

@Component({
    selector: 'app-login',
    standalone: true,
    templateUrl: './login.component.html'
})
export class LoginComponent {
    private auth = inject(AuthService);
    isLoggingIn = signal(false);

    async handleLogin() {
        this.isLoggingIn.set(true);
        // Real implementation would have an input field for the mock email, 
        // but the interceptor explicitly sets 'testuser@example.com' for now.
        await this.auth.login('/');
        this.isLoggingIn.set(false);
    }
}
