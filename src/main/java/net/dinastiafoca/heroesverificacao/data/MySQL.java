package net.dinastiafoca.heroesverificacao.data;

import net.dinastiafoca.heroesverificacao.HeroesVerificacao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQL {

  private final HeroesVerificacao plugin;
  private final String user;
  private final String pass;
  private final String url;

  private Connection connection;

  public MySQL(HeroesVerificacao plugin, String host, int port, String database, String user, String pass) {
    this.plugin = plugin;
    this.user = user;
    this.pass = pass;
    this.url = "jdbc:mysql://" + host + ":" + port + "/" + database;
  }

  public boolean connect() {
    try {
      connection = DriverManager.getConnection(url, user, pass);
      plugin.getLogger().fine("MySQL conectado com sucesso!");
      return true;
    }
    catch(SQLException exception) {
      plugin.getLogger().warning("Falha ao conectar com o MySQL: " + url);
      exception.printStackTrace();
      return false;
    }
  }

  public void disconnect() {
    if(connection != null) {
      try {
        connection.close();
        plugin.getLogger().info("MySQL desconectado com sucesso!");
      }
      catch(SQLException e) {
        plugin.getLogger().warning("Falha ao se desconectar do MySQL: " + url);
        e.printStackTrace();
      }
    }
  }

  public boolean isConnected() {
    if(connection == null) {
      return false;
    }

    try{
      Statement stm = connection.createStatement();
      ResultSet rs = stm.executeQuery("SELECT 1");
      return true;
    }
    catch(Exception ex) { }

    return false;
  }

  public Connection getConnection() {
    if (connection == null || !isConnected()) {
      plugin.getLogger().warning("MySQL desconectado, reconectando...");
      connection = null;
      connect();
    }

    return connection;
  }
}