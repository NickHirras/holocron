import { Routes } from '@angular/router';
import { LandingComponent } from './landing/landing.component';
import { LoginComponent } from './login/login.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { CeremonyCreator } from './ceremony-creator/ceremony-creator';
import { CeremonyResponderComponent } from './ceremony-responder/ceremony-responder.component';
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
    // The "no team yet" dashboard or redirector
    {
        path: 'dashboard',
        component: DashboardComponent,
        canActivate: [authGuard]
    },
    // Team-specific routes
    {
        path: ':teamId',
        canActivate: [authGuard],
        children: [
            {
                path: 'dashboard',
                component: DashboardComponent
            },
            {
                path: 'create',
                component: CeremonyCreator
            },
            {
                path: 'ceremony/:id',
                component: CeremonyResponderComponent
            },
            {
                path: 'create/:id/results',
                loadComponent: () => import('./ceremony-results/ceremony-results.component').then(m => m.CeremonyResultsComponent)
            }
        ]
    }
];
