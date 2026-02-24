import { Component, inject, signal } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { FormsModule } from '@angular/forms';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [FormsModule],
    templateUrl: './login.component.html'
})
export class LoginComponent {
    private auth = inject(AuthService);
    isLoggingIn = signal(false);
    email = signal('testuser@example.com');

    async handleLogin() {
        if (!this.email()) return;
        this.isLoggingIn.set(true);
        await this.auth.login(this.email(), '/');
        this.isLoggingIn.set(false);
    }
}
