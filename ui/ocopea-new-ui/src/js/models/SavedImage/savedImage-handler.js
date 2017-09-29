import SavedImageApi from './savedImage-api';
import SavedImageService from './savedImage-service';

export default class SavedImageHandler {

  static fetchSavedImages() {
    SavedImageService.fetchSavedImages(SavedImageApi.savedImage);
  }
}
