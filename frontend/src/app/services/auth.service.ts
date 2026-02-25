import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { USER_CLIENT } from '../app.config';
import { User } from '../../proto-gen/holocron/v1/ceremony_pb';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private userClient = inject(USER_CLIENT);
    private router = inject(Router);

    // State
    private readonly _userProfile = signal<User | undefined>(undefined);
    public readonly availableProviders = signal<string[]>([]);

    // Publicly exposed readonly state derived from the private signal
    public readonly userProfile = this._userProfile.asReadonly();
    public readonly isLoggedIn = computed(() => {
        if (this._userProfile() !== undefined) return true;
        if (typeof window !== 'undefined' && window.localStorage) {
            return !!localStorage.getItem('holocron_jwt');
        }
        return false;
    });

    constructor() {
        if (typeof window !== 'undefined' && window.localStorage) {
            if (localStorage.getItem('holocron_jwt')) {
                this.refreshProfile();
            }
        }
        this.fetchProviders();
    }

    private async fetchProviders() {
        try {
            const res = await fetch('http://localhost:8080/api/auth/providers');
            if (res.ok) {
                const providers = await res.json();
                this.availableProviders.set(providers);
            }
        } catch (e) {
            console.error('❌ Failed to fetch auth providers:', e);
        }
    }

    private async refreshProfile() {
        try {
            const resp = await this.userClient.getSelf({});
            this._userProfile.set(resp.user);
        } catch (e) {
            console.error('❌ Failed to refresh profile:', e);
            this._userProfile.set(undefined);
            if (typeof window !== 'undefined' && window.localStorage) {
                localStorage.removeItem('holocron_jwt');
            }
        }
    }

    async handleCallbackToken(token: string) {
        if (typeof window !== 'undefined' && window.localStorage) {
            localStorage.setItem('holocron_jwt', token);
        }
        await this.refreshProfile();
    }

    loginWithProvider(provider: string, emailHint?: string) {
        let url = `http://localhost:8080/api/auth/login/${provider}`;
        if (emailHint) {
            url += `?email=${encodeURIComponent(emailHint)}`;
        }
        window.location.href = url;
    }

    logout() {
        this._userProfile.set(undefined);
        if (typeof window !== 'undefined' && window.localStorage) {
            localStorage.removeItem('holocron_jwt');
        }
        this.router.navigate(['/login']);
    }
}
