import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
    selector: 'app-landing',
    standalone: true,
    templateUrl: './landing.component.html'
})
export class LandingComponent {
    private router = inject(Router);
    private auth = inject(AuthService);

    getStarted() {
        if (this.auth.isLoggedIn()) {
            // If already logged in, maybe go to a dashboard (we'll just alert for now, or route to /dashboard if it existed)
            alert("You are already logged in! Proceeding to the application...");
        } else {
            this.router.navigate(['/login']);
        }
    }
}
