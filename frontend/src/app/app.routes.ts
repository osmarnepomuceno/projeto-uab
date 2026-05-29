import { Routes } from '@angular/router';
import { AdminGuard } from './core/guards/admin.guard';
import { AuthGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./modules/login/login.component').then(m => m.LoginComponent) },
  {
    path: '',
    loadComponent: () => import('./shared/components/app-shell.component').then(m => m.AppShellComponent),
    canActivate: [AuthGuard],
    children: [
      { path: 'dashboard', loadComponent: () => import('./modules/dashboard/dashboard.component').then(m => m.DashboardComponent) },
      { path: 'associados', loadComponent: () => import('./modules/associados/associados-list.component').then(m => m.AssociadosListComponent) },
      { path: 'boletos', loadComponent: () => import('./modules/boletos/boletos.component').then(m => m.BoletosComponent) },
      {
        path: 'usuarios',
        loadComponent: () => import('./modules/usuarios/usuarios.component').then(m => m.UsuariosComponent),
        canActivate: [AdminGuard]
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  { path: 'unauthorized', loadComponent: () => import('./shared/components/unauthorized.component').then(m => m.UnauthorizedComponent) },
  { path: '**', redirectTo: 'dashboard' }
];
