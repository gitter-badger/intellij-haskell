/*
 * Copyright 2016 Rik van der Kleij
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package intellij.haskell.settings

import intellij.haskell.HaskellNotificationGroup

object HaskellSettingsState {
  private def state = HaskellSettings.getInstance().getState

  def getHaskellDocsPath: Option[String] = {
    val path = findPath(state.haskellDocsPath)
    notifyIfPathIsNotSet(path, HaskellConfigurable.HaskellDocs)
    path
  }

  def setHaskellDocsPath(haskellDocsPath: String) {
    state.haskellDocsPath = haskellDocsPath
  }

  def getHLintPath: Option[String] = {
    val path = findPath(state.hlintPath)
    notifyIfPathIsNotSet(path, HaskellConfigurable.Hlint)
    path
  }

  def setHlintPath(hlintPath: String) {
    state.hlintPath = hlintPath
  }

  private def notifyIfPathIsNotSet(path: Option[String], name: String) {
    if (path.isEmpty) {
      HaskellNotificationGroup.notifyBalloonError("Path to `" + name + "` is not set")
    }
  }

  private def findPath(path: String): Option[String] = {
    Option(path).filterNot(_.trim.isEmpty)
  }
}
