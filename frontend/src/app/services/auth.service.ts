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

    // Publicly exposed readonly state derived from the private signal
    public readonly userProfile = this._userProfile.asReadonly();
    public readonly isLoggedIn = computed(() => {
        if (this._userProfile() !== undefined) return true;
        if (typeof window !== 'undefined' && window.localStorage) {
            return localStorage.getItem('mockLoggedIn') === 'true';
        }
        return false;
    });

    constructor() {
        if (typeof window !== 'undefined' && window.localStorage) {
            if (localStorage.getItem('mockLoggedIn') === 'true') {
                this.refreshProfile();
            }
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
                localStorage.removeItem('mockLoggedIn');
            }
        }
    }

    async login(email: string, redirectUrl: string = '/') {
        try {
            if (typeof window !== 'undefined' && window.localStorage) {
                localStorage.setItem('mockUserEmail', email);
                localStorage.setItem('mockLoggedIn', 'true');
            }

            // The interceptor automatically attaches the 'x-mock-user-id' header
            const resp = await this.userClient.getSelf({});
            this._userProfile.set(resp.user);
            console.log('✅ Authenticated as:', resp.user?.email);
            await this.router.navigateByUrl(redirectUrl);
        } catch (e) {
            console.error('❌ Failed to authenticate:', e);
            this._userProfile.set(undefined);
            if (typeof window !== 'undefined' && window.localStorage) {
                localStorage.removeItem('mockLoggedIn');
            }
        }
    }

    logout() {
        this._userProfile.set(undefined);
        if (typeof window !== 'undefined' && window.localStorage) {
            localStorage.removeItem('mockLoggedIn');
            localStorage.removeItem('mockUserEmail');
        }
        this.router.navigate(['/login']);
    }
}
