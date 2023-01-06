package ol.tools;

import arc.backend.sdl.SdlApplication;
import arc.backend.sdl.SdlConfig;
import arc.func.Intp;
import arc.util.ArcNativesLoader;
import arc.util.Log;
import arc.util.OS;

import static arc.backend.sdl.jni.SDL.*;

public class GlSetup {

    private static void check(Intp run){
        if(run.get() != 0){
            throw new SdlApplication.SdlError();
        }
    }
    public static long init(){
        SdlConfig config = new SdlConfig() {{
//            title = "Mindustry";
//            maximized = true;
//            width = 900;
//            height = 700;
            //enable gl3 with command-line argument (slower performance, apparently)
           /* if(Structs.contains(arg, "-gl3")){
                gl30 = true;
            }*/
            /*if(Structs.contains(arg, "-antialias")){
                samples = 16;
            }*/
            samples = 16;
            /*if(Structs.contains(arg, "-debug")){
                Log.level = Log.LogLevel.debug;
            }
            setWindowIcon(Files.FileType.internal, "icons/icon_64.png");*/
        }};

        ArcNativesLoader.load();

        check(() -> SDL_Init(SDL_INIT_VIDEO | SDL_INIT_EVENTS));

        //show native IME candidate UI
        SDL_SetHint("SDL_IME_SHOW_UI","1");

        //set up openGL 2.0 profile
        check(() -> SDL_GL_SetAttribute(SDL_GL_CONTEXT_MAJOR_VERSION, config.gl30 ? config.gl30Major : 2));
        check(() -> SDL_GL_SetAttribute(SDL_GL_CONTEXT_MINOR_VERSION,  config.gl30 ? config.gl30Minor : 0));

        if(config.gl30 && OS.isMac){
            check(() -> SDL_GL_SetAttribute(SDL_GL_CONTEXT_PROFILE_MASK, SDL_GL_CONTEXT_PROFILE_CORE));
        }

        check(() -> SDL_GL_SetAttribute(SDL_GL_RED_SIZE, config.r));
        check(() -> SDL_GL_SetAttribute(SDL_GL_GREEN_SIZE, config.g));
        check(() -> SDL_GL_SetAttribute(SDL_GL_BLUE_SIZE, config.b));
        check(() -> SDL_GL_SetAttribute(SDL_GL_DEPTH_SIZE, config.depth));
        check(() -> SDL_GL_SetAttribute(SDL_GL_STENCIL_SIZE, config.stencil));
        check(() -> SDL_GL_SetAttribute(SDL_GL_DOUBLEBUFFER, 1));

        //this doesn't seem to do anything, but at least I tried
        if(config.samples > 0){
            check(() -> SDL_GL_SetAttribute(SDL_GL_MULTISAMPLEBUFFERS, 1));
            check(() -> SDL_GL_SetAttribute(SDL_GL_MULTISAMPLESAMPLES, config.samples));
        }

        int flags = SDL_WINDOW_OPENGL;
        if(config.initialVisible) flags |= SDL_WINDOW_SHOWN;
        if(!config.decorated) flags |= SDL_WINDOW_BORDERLESS;
        if(config.resizable) flags |= SDL_WINDOW_RESIZABLE;
        if(config.maximized) flags |= SDL_WINDOW_MAXIMIZED;
        if(config.fullscreen) flags |= SDL_WINDOW_FULLSCREEN;

        long window = SDL_CreateWindow(config.title, 1,1, flags);
        if(window == 0) throw new SdlApplication.SdlError();

       long  context = SDL_GL_CreateContext(window);
        if(context == 0) throw new SdlApplication.SdlError();

        if(config.vSyncEnabled){
            SDL_GL_SetSwapInterval(1);
        }

        int[] ver = new int[3];
        SDL_GetVersion(ver);
        Log.info("[Core] Initialized SDL v@.@.@", ver[0], ver[1], ver[2]);
        return window;
    }

    public static void disable(long window){
        SDL_DestroyWindow(window);
    }
}
