package cz.seznam.euphoria.core.util;

import cz.seznam.euphoria.core.client.operator.Pair;

import java.io.Serializable;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * General utility class to store key/value pairs as strings providing converter
 * methods for primitives and frequently used types. Aims to help with presenting
 * a set of configuration/setting values.
 */
public class Settings implements Serializable {

  private final String prefix;
  private final Map<String, String> map;


  public Settings() {
    this(null, new ConcurrentHashMap<>());
  }

  private Settings(String prefix, Map<String, String> map) {
    this.prefix = prefix;
    this.map = map;
  }

  private String skey(String key) {
    return prefix == null ? key : prefix + key;
  }

  /**
   * Returns a nested view on key/values with the given key prefix. The returned
   * {@link Settings} instance will shared the underlying storage with its parent,
   * and will automatically and transparently strip/add the specified key prefix.
   *
   * @param prefix the key prefix defining the nesting of the view in the parent storage
   *
   * @return a "prefixed" view of this settings instance
   */
  public Settings nested(String prefix) {
    if (prefix == null || prefix.isEmpty()) {
      return this;
    }
    if (!prefix.endsWith(".")) {
      prefix = prefix + ".";
    }
    return new Settings(prefix, map);
  }

  public Map<String, String> getAll() {
    if (prefix == null) {
      return map;
    }
    return map.entrySet()
        .stream()
        .filter(e -> e.getKey().startsWith(prefix))
        .map(e -> Pair.of(e.getKey().substring(prefix.length()), e.getValue()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public boolean contains(String key) {
    return containsSkey(skey(requireNonNull(key)));
  }

  private boolean containsSkey(String skey) {
    return map.containsKey(skey);
  }

  // STRING ------------------------------------------------------------------------------
  public void setString(String key, String value) {
    map.put(skey(requireNonNull(key)), requireNonNull(value));
  }

  public String getString(String key, String def) {
    String skey = skey(requireNonNull(key));
    return map.containsKey(skey) ? map.get(skey) : def;
  }

  public String getString(String key) {
    String skey = skey(requireNonNull(key));
    if (!containsSkey(skey)) {
      throw new IllegalArgumentException(
          "No value for: " + key + " (settings prefix: " + prefix + ")");
    }
    return map.get(skey);
  }

  // BOOL --------------------------------------------------------------------------------
  public void setBoolean(String key, boolean value) {
    setString(key, Boolean.toString(value));
  }

  public boolean getBoolean(String key, boolean def) {
    String stringVal = getString(key, null);
    return stringVal == null ? def :  Boolean.parseBoolean(stringVal);
  }
  
  public boolean getBoolean(String key) {
    String stringVal = getString(key);
    return Boolean.parseBoolean(stringVal);
  }

  // INT ---------------------------------------------------------------------------------
  public void setInt(String key, int value) {
    setString(key, String.valueOf(value));
  }
  
  public int getInt(String key, int def) {
    String stringVal = getString(key, null);
    return stringVal == null ? def : Integer.parseInt(stringVal);
  }
  
  public int getInt(String key) {
    String stringVal = getString(key);
    return Integer.parseInt(stringVal);
  }
  
  // LONG --------------------------------------------------------------------------------
  public void setLong(String key, long value) {
    setString(key, String.valueOf(value));
  }
  
  public long getLong(String key, long def) {
    String stringVal = getString(key, null);
    return stringVal == null ? def : Long.parseLong(stringVal);
  }
  
  public long getLong(String key) {
    String stringVal = getString(key);
    return Long.parseLong(stringVal);
  }

  // URI ---------------------------------------------------------------------------------
  public void setURI(String key, URI uri) {
    setString(key, uri.toString());
  }
  
  public URI getURI(String key, URI def) {
    String stringVal = getString(key, null);
    return stringVal == null ? def : URI.create(stringVal);
  }

  public URI getURI(String key) {
    String stringVal = getString(key);
    return URI.create(stringVal);
  }

  // CLASS -------------------------------------------------------------------------------
  public void setClass(String key, Class<?> cls) {
    setString(key, cls.getName());
  }

  public <E> Class<? extends E> getClass(String key, Class<E> superType) {
    String className = getString(key);
    return InstanceUtils.forName(className, superType);
  }
}
