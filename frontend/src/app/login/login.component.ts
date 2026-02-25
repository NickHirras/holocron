import { Component, inject, signal, OnInit } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { FormsModule } from '@angular/forms';
import { TitleCasePipe } from '@angular/common';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [FormsModule, TitleCasePipe],
    templateUrl: './login.component.html'
})
export class LoginComponent implements OnInit {
    public auth = inject(AuthService);

    isLoggingIn = signal(false);
    mockEmail = signal('testuser@example.com');

    ngOnInit() {
        if (typeof window !== 'undefined') {
            const params = new URLSearchParams(window.location.search);
            const token = params.get('token');
            if (token) {
                this.isLoggingIn.set(true);
                this.auth.handleCallbackToken(token).then(() => {
                    window.location.href = '/';
                });
            }
        }
    }

    handleLogin(provider: string) {
        this.isLoggingIn.set(true);
        if (provider === 'mock') {
            this.auth.loginWithProvider(provider, this.mockEmail());
        } else {
            this.auth.loginWithProvider(provider);
        }
    }
}
