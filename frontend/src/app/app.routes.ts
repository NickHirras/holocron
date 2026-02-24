import { Routes } from '@angular/router';
import { LandingComponent } from './landing/landing.component';
import { LoginComponent } from './login/login.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { CeremonyCreator } from './ceremony-creator/ceremony-creator';
import { guestGuard } from './guards/guest.guard';
import { authGuard } from './guards/auth-guard';

export const routes: Routes = [
    {
        path: '',
        component: LandingComponent,
        canActivate: [guestGuard]
    },
    {
        path: 'login',
        component: LoginComponent,
        canActivate: [guestGuard]
    },
    {
        path: 'dashboard',
        component: DashboardComponent,
        canActivate: [authGuard]
    },
    {
        path: 'create',
        component: CeremonyCreator,
        canActivate: [authGuard]
    }
];
