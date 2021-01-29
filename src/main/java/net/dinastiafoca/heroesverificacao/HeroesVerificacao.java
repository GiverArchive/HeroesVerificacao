package net.dinastiafoca.heroesverificacao;

import net.dinastiafoca.heroesverificacao.command.VerificarCommand;
import net.dinastiafoca.heroesverificacao.data.MySQL;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.sql.Statement;

public final class HeroesVerificacao extends JavaPlugin {

  private MySQL mysql;
  private String pendingTableName;
  private String verifiedTableName;
  private String nickColumn;
  private String discordIdColumn;
  private String verifiedColumn;
  private String codeColumn;

  @Override
  public void onEnable() {
    if(!getDataFolder().exists()) {
      saveDefaultConfig();
      getLogger().warning("MySQL ainda não foi configurado!");
      setEnabled(false);
      return;
    }

    String host = getConfig().getString("MySQL.Host");
    String user = getConfig().getString("MySQL.Username");
    String pass = getConfig().getString("MySQL.Password");
    String database = getConfig().getString("MySQL.Database");
    int port = getConfig().getInt("MySQL.Port");

    mysql = new MySQL(this, host, port, database, user, pass);

    if(!mysql.connect()) {
      getLogger().warning("Verifique a conexão e as credenciais!");
      this.setEnabled(false);
      return;
    }

    pendingTableName = getConfig().getString("Schema.PendingTableName");
    verifiedTableName = getConfig().getString("Schema.VerifiedTableName");
    nickColumn = getConfig().getString("Schema.ColumnNick");
    discordIdColumn = getConfig().getString("Schema.ColumnID");
    verifiedColumn = getConfig().getString("Schema.ColumnVerified");
    codeColumn = getConfig().getString("Schema.ColumnCode");

    createDefaultTables();
    new VerificarCommand(this);
  }

  @Override
  public void onDisable() {
    if(mysql != null && mysql.isConnected()) {
      mysql.disconnect();
    }
  }

  private void createDefaultTables() {
    String pendingTable = "CREATE TABLE IF NOT EXISTS `$tableName` (`$nick` VARCHAR(16) NOT NULL, `$discordId` BIGINT(20) NOT NULL, `$code` VARCHAR(32) NOT NULL);";
    String verifiedTable = "CREATE TABLE IF NOT EXISTS `$tableName` (`$nick` VARCHAR(16) NOT NULL, `$discordId` BIGINT(20) NOT NULL, `$verified` TINYINT(1) NOT NULL);";

    pendingTable = pendingTable
            .replace("$tableName", pendingTableName)
            .replace("$nick", nickColumn)
            .replace("$discordId", discordIdColumn)
            .replace("$code", codeColumn);

    verifiedTable = verifiedTable
            .replace("$tableName", verifiedTableName)
            .replace("$nick", nickColumn)
            .replace("$discordId", discordIdColumn)
            .replace("$verified", verifiedColumn);

    try {
      Statement stm = mysql.getConnection().createStatement();
      stm.executeUpdate(pendingTable);
      stm.executeUpdate(verifiedTable);
    } catch(SQLException throwables) {
      throwables.printStackTrace();
      getLogger().warning("Falha criar tabela no MySQL");
    }
  }

  public MySQL getMysql() {
    return mysql;
  }

  public String getPendingTableName() {
    return pendingTableName;
  }

  public String getVerifiedTableName() {
    return verifiedTableName;
  }

  public String getNickColumn() {
    return nickColumn;
  }

  public String getDiscordIdColumn() {
    return discordIdColumn;
  }

  public String getVerifiedColumn() {
    return verifiedColumn;
  }

  public String getCodeColumn() {
    return codeColumn;
  }
}
