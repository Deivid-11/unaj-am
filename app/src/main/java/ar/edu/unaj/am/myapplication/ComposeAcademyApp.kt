package ar.edu.unaj.am.myapplication

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * @HiltAndroidApp es el punto de partida de la inyección de dependencias:
 * genera el componente raíz (SingletonComponent) y lo deja vivo mientras viva la app.
 *
 * Sin esta anotación, ningún @AndroidEntryPoint ni @HiltViewModel funciona.
 */
@HiltAndroidApp
class ComposeAcademyApp : Application()
