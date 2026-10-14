import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { RUNTIME_CONFIG } from '../../config/runtime.config';
@Component({ selector: 'app-administration', standalone: true, imports: [CommonModule], templateUrl: './administration.html', styleUrl: '../operations.scss' })
export class Administration {
  readonly auth = inject(AuthService); readonly runtime = RUNTIME_CONFIG;
}
