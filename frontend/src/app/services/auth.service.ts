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
    public readonly isLoggedIn = computed(() => this._userProfile() !== undefined);

    async login(redirectUrl: string = '/') {
        try {
            // The interceptor automatically attaches the 'x-mock-user-id' header
            const resp = await this.userClient.getSelf({});
            this._userProfile.set(resp.user);
            console.log('✅ Authenticated as:', resp.user?.email);
            await this.router.navigateByUrl(redirectUrl);
        } catch (e) {
            console.error('❌ Failed to authenticate:', e);
            this._userProfile.set(undefined);
        }
    }

    logout() {
        this._userProfile.set(undefined);
        this.router.navigate(['/login']);
    }
}
