package jetbrains.buildServer.artifacts;

import jetbrains.buildServer.controllers.BuildDataExtensionUtil;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactHolder;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.SimplePageExtension;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Nikita.Skvortsov
 * date: 18.02.2016.
 */
public class S3PopupList extends SimplePageExtension {

  private final SBuildServer myServer;

  public S3PopupList(@NotNull PagePlaces pagePlaces,
                     @NotNull SBuildServer server,
                     @NotNull PluginDescriptor descriptor) {
    super(pagePlaces, PlaceId.BUILD_ARTIFACTS_POPUP_FRAGMENT, "s3_popup", descriptor.getPluginResourcesPath("s3_popup.jsp"));
    myServer = server;
    register();
  }

  @Override
  public boolean isAvailable(@NotNull HttpServletRequest request) {
    final SBuild sBuild = BuildDataExtensionUtil.retrieveBuild(request, myServer);
    return sBuild != null && sBuild.getArtifacts(BuildArtifactsViewMode.VIEW_ALL).findArtifact(Constants.S3_ARTIFACTS_LIST).isAvailable();
  }

  @Override
  public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
    super.fillModel(model, request);
    final SBuild sBuild = BuildDataExtensionUtil.retrieveBuild(request, myServer);
    if (sBuild == null) {
      return;
    }
    final BuildArtifactHolder artifact = sBuild.getArtifacts(BuildArtifactsViewMode.VIEW_ALL).findArtifact(Constants.S3_ARTIFACTS_LIST);
    if (!artifact.isAvailable()) {
      return;
    } else {
      try {
        final Map<String, String> urlsToNames = S3Util.readArtifactsUrls(artifact.getArtifact().getInputStream()).entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue,
            entry -> entry.getKey().substring(entry.getKey().lastIndexOf("/") + 1)));
        model.put("urlsToNames", urlsToNames);
      } catch (IOException e) {
        Loggers.SERVER.warnAndDebugDetails("Failed to read s3 artifacts list for build " + sBuild.getBuildDescription() + ". See debug logs for details", e);
      }
    }
  }
}