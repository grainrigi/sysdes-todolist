package models

import java.sql.Timestamp

import utility.Digest

/**
  * Domain model of user
  * @param id          ID
  * @param name       ユーザー名
  * @param password パスワードのSHA256ハッシュ
  * @param passwordSalt      パスワードのソルト
  */
case class User(id: Int, name: String, password: String, passwordSalt: String) {
  def comparePassword(input: String): Boolean = {
    Digest(input, passwordSalt) == password
  }

  def withPassword(newpass: String): User = {
    val salt = Digest.genSalt
    User(id, name, Digest(newpass, salt), salt)
  }
}

object User extends DomainModel[User] {
  import slick.jdbc.GetResult
  implicit def getResult: GetResult[User] = GetResult(
    r => User(r.nextInt, r.nextString, r.nextString, r.nextString)
  )

  def apply(name: String, password: String): User =
    User(0, name, "", "")
      .withPassword(password)
}
