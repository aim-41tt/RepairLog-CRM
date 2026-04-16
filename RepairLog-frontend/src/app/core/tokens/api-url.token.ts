import { InjectionToken } from '@angular/core';

//export const API_URL = new InjectionToken<string>('API_URL', {
//  providedIn: 'root',
//  factory: () => `http://${window.location.hostname}:8080/api`
//});

export const API_URL = new InjectionToken<string>('API_URL', {
  providedIn: 'root',
  factory: () => '/api'
});