package me.guard.car;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class Marshal {

  public static void dump(OutputStream out, Object o) {
    ObjectOutputStream objectStream = null;
    try {
      objectStream = new ObjectOutputStream(out);
      objectStream.writeObject(o);
    } catch (IOException ignored) {
    } finally {
      if (objectStream != null) {
        try {
          objectStream.close();
        } catch (IOException ignored) {
        }
      }
    }
  }

  public static Object load(InputStream in) {
    ObjectInputStream objectStream = null;

    try {
      objectStream = new ObjectInputStream(in);
      return objectStream.readObject();
    } catch (IOException ignored) {
    } catch (ClassNotFoundException ignored) {
    } finally {
      if (objectStream != null) {
        try {
          objectStream.close();
        } catch (IOException ignored) {
        }
      }
    }

    return null;
  }
}
