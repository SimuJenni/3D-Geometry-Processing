package openGL.interfaces;

import openGL.objects.RenderItem;

/**
 * An iterator to traverse scenes. It returns objects of type {@link RenderItem}
 * , which bundle information about shapes and their transformations.
 */
public interface SceneManagerIterator {

	public boolean hasNext();

	public RenderItem next();
}
