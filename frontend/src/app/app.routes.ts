import { Routes } from '@angular/router';
import { LandingComponent } from './landing/landing.component';
import { LoginComponent } from './login/login.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { authGuard } from './guards/auth-guard';
import { guestGuard } from './guards/guest.guard';

export const routes: Routes = [
    { path: '', component: LandingComponent, canActivate: [guestGuard] },
    { path: 'login', component: LoginComponent, canActivate: [guestGuard] },
    { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] }
];
