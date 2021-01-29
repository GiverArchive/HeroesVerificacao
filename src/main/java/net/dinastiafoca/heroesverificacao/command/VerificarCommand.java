package net.dinastiafoca.heroesverificacao.command;

import net.dinastiafoca.heroesverificacao.HeroesVerificacao;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class VerificarCommand implements CommandExecutor {

  private final HeroesVerificacao plugin;

  public VerificarCommand(HeroesVerificacao plugin) {
    this.plugin = plugin;

    plugin.getCommand("verificar").setExecutor(this);
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if(!(sender instanceof Player)) {
      sender.sendMessage(ChatColor.RED + "Este comando só pode ser executado por jogadores!");
      return true;
    }

    if(!plugin.getMysql().isConnected()) {
      sender.sendMessage(ChatColor.RED + "Erro interno, por favor, contate um Administrador!");
      return true;
    }

    if(args.length == 0) {
      sender.sendMessage(ChatColor.RED + "Uso correto do comando: /verificar <codigo>");
      return true;
    }

    if(isVerified(sender.getName())) {
      sender.sendMessage(ChatColor.RED + "Você já foi verificado.");
      return true;
    }

    if(!verify(sender, args[0])) {
      sender.sendMessage(ChatColor.RED + "Erro interno ao verificar, por favor, contate um Administrador!");
      return true;
    }

    return true;
  }

  private boolean isVerified(String nick) {
    String sql = "SELECT * FROM " + plugin.getVerifiedTableName() + " WHERE " + plugin.getNickColumn() + "=?";

    try{
      PreparedStatement stm = plugin.getMysql().getConnection().prepareStatement(sql);
      stm.setString(1, nick);

      ResultSet rs = stm.executeQuery();

      if(!rs.next()) {
        return false;
      }

      return rs.getBoolean(3);
    }
    catch(Exception ex) {
      ex.printStackTrace();
      return false;
    }
  }

  private boolean verify(CommandSender sender, String code) {
    String sql = "SELECT * FROM " + plugin.getPendingTableName() + " WHERE " + plugin.getNickColumn() + "=?";

    try{
      PreparedStatement stm = plugin.getMysql().getConnection().prepareStatement(sql);
      stm.setString(1, sender.getName());

      ResultSet rs = stm.executeQuery();

      if(!rs.next()) {
        sender.sendMessage(ChatColor.RED + "Você não iniciou uma verificação dentro no servidor do Discord!");
        return true;
      }

      String needCode = rs.getString(3);

      if(!needCode.equals(code)) {
        sender.sendMessage(ChatColor.RED + "Código de verificação inválido!");
        return true;
      }

      String removeSql = "DELETE FROM " + plugin.getPendingTableName() + " WHERE " + plugin.getNickColumn() + "=?";
      PreparedStatement removeFromPending = plugin.getMysql().getConnection().prepareStatement(removeSql);
      removeFromPending.setString(1, sender.getName());
      removeFromPending.execute();

      String addSql = "INSERT INTO " + plugin.getVerifiedTableName() + "(" + plugin.getNickColumn() + ", " + plugin.getDiscordIdColumn() + ", " + plugin.getVerifiedColumn() + ") VALUES (?, ?, ?);";
      PreparedStatement addToVerified = plugin.getMysql().getConnection().prepareStatement(addSql);
      addToVerified.setString(1, sender.getName());
      addToVerified.setLong(2, rs.getLong(2));
      addToVerified.setBoolean(3, true);
      addToVerified.execute();

      sender.sendMessage(ChatColor.GREEN + "Verificado com sucesso!");

      for(String cmd : plugin.getConfig().getStringList("OnVerifyCommands")) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("$nick", sender.getName()));
      }

      return true;
    }
    catch(Exception ex) {
      ex.printStackTrace();
      return false;
    }
  }
}
