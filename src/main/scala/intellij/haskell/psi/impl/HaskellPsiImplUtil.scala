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

package intellij.haskell.psi.impl

import javax.swing._

import com.intellij.navigation.ItemPresentation
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiElement, PsiReference, TokenType}
import com.intellij.util.ArrayUtil
import intellij.haskell.external.component.StackReplsComponentsManager
import intellij.haskell.psi.HaskellTypes._
import intellij.haskell.psi._
import intellij.haskell.util.StringUtil
import intellij.haskell.{HaskellFileType, HaskellIcons}

import scala.collection.JavaConversions._

object HaskellPsiImplUtil {

  def getName(qVarCon: HaskellQVarCon): String = {
    qVarCon.getText
  }

  def getIdentifierElement(qVarCon: HaskellQVarCon): HaskellNamedElement = {
    Option(qVarCon.getVarid).orElse(Option(qVarCon.getQCon).map(_.getConid)).orElse(Option(qVarCon.getConsym)).orElse(Option(qVarCon.getVarsym)).
      getOrElse(throw new IllegalStateException(s"Identifier for $qVarCon should exist"))
  }

  def getName(varCon: HaskellVarCon): String = {
    varCon.getText
  }

  def getIdentifierElement(varCon: HaskellVarCon): HaskellNamedElement = {
    Option(varCon.getConid).
      orElse(Option(varCon.getConsym)).
      orElse(Option(varCon.getVarid)).
      orElse(Option(varCon.getVarsym)).
      getOrElse(throw new IllegalStateException(s"Identifier for $varCon should exist"))
  }

  def getName(qName: HaskellQName): String = {
    qName.getText
  }

  def getNameWithoutParens(qName: HaskellQName): String = {
    Option(qName.getVarCon).map(_.getName).getOrElse(qName.getQVarCon.getName)
  }

  def getIdentifierElement(qName: HaskellQName): HaskellNamedElement = {
    Option(qName.getVarCon).map(_.getIdentifierElement).
      orElse(Option(qName.getQVarCon).map(_.getIdentifierElement)).
      getOrElse(throw new IllegalStateException(s"Identifier for $qName should exist"))
  }

  def getQualifierName(qName: HaskellQName): Option[String] = {
    Option(qName.getQVarCon).flatMap(qvc =>
      Option(qvc.getQualifier).map(_.getName).
        orElse(Option(qvc.getQCon).flatMap(qc => Option(qc.getQConQualifier1).orElse(Option(qc.getQConQualifier2)).orElse(Option(qc.getQConQualifier3)).map(_.getText))))
  }

  def getName(cname: HaskellCname): String = {
    cname.getText
  }

  def getNameWithoutParens(cname: HaskellCname): String = {
    Option(cname.getVar).map(_.getName).
      orElse(Option(cname.getCon).map(_.getName)).
      orElse(Option(cname.getVarop).map(_.getName)).
      getOrElse(cname.getConop.getName)
  }

  def getIdentifierElement(cname: HaskellCname): HaskellNamedElement = {
    Option(cname.getVar).map(_.getVarsym).
      orElse(Option(cname.getVar).map(_.getVarid)).
      orElse(Option(cname.getVarop).map(_.getVarid)).
      orElse(Option(cname.getVarop).map(_.getVarsym)).
      orElse(Option(cname.getCon).map(_.getConid)).
      orElse(Option(cname.getCon).map(_.getConsym)).
      orElse(Option(cname.getConop).map(_.getConid)).
      orElse(Option(cname.getConop).map(_.getConsym)).
      getOrElse(throw new IllegalStateException(s"Identifier for $cname should exist"))
  }

  def getQualifierName(cname: HaskellCname): Option[String] = {
    None
  }

  def getName(modid: HaskellModid): String = {
    modid.getText
  }

  def getNameIdentifier(modid: HaskellModid): HaskellNamedElement = {
    modid
  }

  def setName(modid: HaskellModid, newName: String): PsiElement = {
    val newModid = HaskellElementFactory.createModid(modid.getProject, removeFileExtension(newName))
    newModid.foreach(mi => modid.getNode.getTreeParent.replaceChild(modid.getNode, mi.getNode))
    modid
  }

  def getName(varid: HaskellVarid): String = {
    varid.getText
  }

  def getNameIdentifier(varId: HaskellVarid): HaskellNamedElement = {
    varId
  }

  def setName(varid: HaskellVarid, newName: String): PsiElement = {
    val newVarid = HaskellElementFactory.createVarid(varid.getProject, newName)
    newVarid.foreach(vid => varid.getNode.getTreeParent.replaceChild(varid.getNode, vid.getNode))
    varid
  }

  def getName(varsym: HaskellVarsym): String = {
    varsym.getText
  }

  def getNameIdentifier(varsym: HaskellVarsym): HaskellNamedElement = {
    varsym
  }

  def setName(varsym: HaskellVarsym, newName: String): PsiElement = {
    val newVarsym = HaskellElementFactory.createVarsym(varsym.getProject, newName)
    newVarsym.foreach(vs => varsym.getNode.getTreeParent.replaceChild(varsym.getNode, vs.getNode))
    varsym
  }

  def getName(conid: HaskellConid): String = {
    conid.getText
  }

  def getNameIdentifier(conid: HaskellConid): HaskellNamedElement = {
    conid
  }

  def setName(conid: HaskellConid, newName: String): PsiElement = {
    val newConid = HaskellElementFactory.createConid(conid.getProject, newName)
    newConid.foreach(ci => conid.getNode.getTreeParent.replaceChild(conid.getNode, ci.getNode))
    conid
  }

  def getName(consym: HaskellConsym): String = {
    consym.getText
  }

  def getNameIdentifier(consym: HaskellConsym): HaskellNamedElement = {
    consym
  }

  def setName(consym: HaskellConsym, newName: String): PsiElement = {
    val newConsym = HaskellElementFactory.createConsym(consym.getProject, newName)
    newConsym.foreach(cs => consym.getNode.getTreeParent.replaceChild(consym.getNode, cs.getNode))
    consym
  }

  def getName(qualifier: HaskellQualifier): String = {
    qualifier.getText
  }

  def getNameIdentifier(qualifier: HaskellQualifier): HaskellNamedElement = {
    qualifier
  }

  def setName(qualifier: HaskellQualifier, newName: String): PsiElement = {
    val newQualifier = HaskellElementFactory.createQualifier(qualifier.getProject, removeFileExtension(newName))
    newQualifier.foreach(q => qualifier.getNode.getTreeParent.replaceChild(qualifier.getNode, q.getNode))
    qualifier
  }

  def getName(qConQualifier: HaskellQualifierElement): String = {
    qConQualifier.getText
  }

  def getNameIdentifier(qConQualifier: HaskellQualifierElement): HaskellNamedElement = {
    qConQualifier
  }

  def setName(qualifier: HaskellQualifierElement, newName: String): PsiElement = {
    val newQualifier = HaskellElementFactory.createQConQualifier(qualifier.getProject, newName)
    newQualifier.foreach(q => qualifier.getNode.getTreeParent.replaceChild(qualifier.getNode, q.getNode))
    qualifier
  }

  def getReference(element: PsiElement): PsiReference = {
    ArrayUtil.getFirstElement(ReferenceProvidersRegistry.getReferencesFromProviders(element))
  }

  def getName(`var`: HaskellVar): String = {
    Option(`var`.getVarid).getOrElse(`var`.getVarsym).getName
  }

  def getName(con: HaskellCon): String = {
    Option(con.getConid).getOrElse(con.getConsym).getName
  }

  def getName(varop: HaskellVarop): String = {
    Option(varop.getVarid).getOrElse(varop.getVarsym).getName
  }

  def getName(conop: HaskellConop): String = {
    Option(conop.getConid).getOrElse(conop.getConsym).getName
  }

  private abstract class HaskellItemPresentation(haskellElement: PsiElement) extends ItemPresentation {

    def getLocationString: String = {
      val file = haskellElement.getContainingFile
      HaskellPsiUtil.findModuleDeclaration(file).map(_.getModid.getName).getOrElse("Unknown module")
    }

    def getIcon(unused: Boolean): Icon = {
      findIcon(haskellElement)
    }

    protected def findIcon(element: PsiElement): Icon = {
      import intellij.haskell.HaskellIcons._
      element match {
        case _: HaskellTypeDeclaration => Type
        case _: HaskellDataDeclaration => Data
        case _: HaskellNewtypeDeclaration => NewType
        case _: HaskellClassDeclaration => Class
        case _: HaskellInstanceDeclaration => Instance
        case _: HaskellDefaultDeclaration => Default
        case _: HaskellTypeSignature => HaskellSmallBlueLogo
        case _: HaskellForeignDeclaration => Foreign
        case _: HaskellTypeFamilyDeclaration => TypeFamily
        case _: HaskellTypeInstanceDeclaration => TypeInstance
        case _: HaskellModuleDeclaration => Module
        case _ => HaskellSmallBlueLogo
      }
    }
  }

  def getPresentation(namedElement: HaskellNamedElement): ItemPresentation = {

    new HaskellItemPresentation(namedElement) {
      val declarationElement = HaskellPsiUtil.findDeclarationElementParent(namedElement)

      def getPresentableText: String = {
        declarationElement.map(e => {
          val declarationInfo = getDeclarationInfo(e)
          if (declarationInfo.contains(namedElement.getName)) {
            declarationInfo
          } else {
            val info = findNameInfo(namedElement).getOrElse(declarationInfo)
            s"${namedElement.getName} `in`  $info"
          }
        }).orElse(HaskellPsiUtil.findImportDeclarationParent(namedElement).flatMap(_.getModuleName).map(n => s"import $n")).
          orElse(findNameInfo(namedElement)).
          map(_.replaceAll("""\s+""", " ")).
          getOrElse(namedElement.getName)
      }

      override def getIcon(unused: Boolean): Icon = {
        declarationElement match {
          case Some(de: HaskellDeclarationElement) => findIcon(de)
          case _ => HaskellIcons.HaskellSmallBlueLogo
        }
      }
    }
  }

  def getPresentation(declarationElement: HaskellDeclarationElement): ItemPresentation = {

    new HaskellItemPresentation(declarationElement) {
      def getPresentableText: String = {
        getDeclarationInfo(declarationElement)
      }
    }
  }

  private def findNameInfo(namedElement: HaskellNamedElement) = {
    StackReplsComponentsManager.findNameInfo(namedElement).headOption.map(_.declaration)
  }

  private final val EndOfDeclarationInfoIndicators = Seq(HS_NEWLINE, HS_EQUAL, HS_WHERE)

  private def getDeclarationInfo(declarationElement: HaskellDeclarationElement): String = {
    val info = declarationElement match {
      case md: HaskellModuleDeclaration => s"module ${md.getModid.getName}"
      case cd: HaskellClassDeclaration => cd.getIdentifierElements.headOption.flatMap(findNameInfo).getOrElse(getTruncatedInfo(cd))
      case dd: HaskellDataDeclaration => dd.getIdentifierElements.headOption.flatMap(findNameInfo).getOrElse(getTruncatedInfo(dd))
      case de => getTruncatedInfo(declarationElement)
    }
    StringUtil.shortenHaskellDeclaration(info)
  }

  private def getTruncatedInfo(declarationElement: HaskellDeclarationElement) = {
    declarationElement.getNode.getChildren(null).takeWhile(n => !EndOfDeclarationInfoIndicators.contains(n.getElementType)).filterNot(_.getElementType == TokenType.WHITE_SPACE).map(_.getText.trim).mkString(" ")
  }

  def getName(declarationElement: HaskellDeclarationElement): String = {
    declarationElement.getPresentation.getPresentableText
  }

  def getIdentifierElements(typeSignature: HaskellTypeSignature): Seq[HaskellNamedElement] = {
    typeSignature.getQNamesList.flatMap(_.getQNameList).map(_.getIdentifierElement)
  }

  def getIdentifierElements(dataDeclaration: HaskellDataDeclaration): Seq[HaskellNamedElement] = {
    dataDeclaration.getSimpletypeList.headOption.map(_.getIdentifierElements).getOrElse(Seq()) ++
      Option(dataDeclaration.getConstr1List).map(_.flatMap(c => Seq(c.getQName.getIdentifierElement) ++ c.getFielddeclList.flatMap(_.getQNameList.headOption.map(_.getIdentifierElement)))).getOrElse(Seq()) ++
      Option(dataDeclaration.getConstr3List).map(_.map(c => c.getQName.getIdentifierElement)).getOrElse(Seq()) ++
      Option(dataDeclaration.getConstr2List).map(_.flatMap(c => c.getQNameList.headOption.map(_.getIdentifierElement))).getOrElse(Seq()) ++
      Option(dataDeclaration.getConstr4List).map(_.flatMap(c => c.getQNameList.headOption.map(_.getIdentifierElement))).getOrElse(Seq())
  }

  def getIdentifierElements(typeDeclaration: HaskellTypeDeclaration): Seq[HaskellNamedElement] = {
    typeDeclaration.getSimpletype.getIdentifierElements
  }

  def getIdentifierElements(newtypeDeclaration: HaskellNewtypeDeclaration): Seq[HaskellNamedElement] = {
    newtypeDeclaration.getSimpletype.getIdentifierElements
  }

  def getIdentifierElements(classDeclaration: HaskellClassDeclaration): Seq[HaskellNamedElement] = {
    classDeclaration.getQNameList.headOption.map(_.getIdentifierElement).toSeq ++
      Option(classDeclaration.getCidecls).map(_.getTypeSignatureList.flatMap(_.getIdentifierElements)).getOrElse(Seq()) ++
      Option(classDeclaration.getCidecls).map(_.getTypeDeclarationList.flatMap(_.getIdentifierElements)).getOrElse(Seq())
  }

  def getIdentifierElements(instanceDeclaration: HaskellInstanceDeclaration): Seq[HaskellNamedElement] = {
    Seq(instanceDeclaration.getQName.getIdentifierElement) ++
      Option(instanceDeclaration.getInst).map(inst => findTypes(inst)).getOrElse(Seq())
  }

  def getIdentifierElements(typeFamilyDeclaration: HaskellTypeFamilyDeclaration): Seq[HaskellNamedElement] = {
    typeFamilyDeclaration.getTypeFamilyType.getQNameList.map(_.getIdentifierElement) ++
      typeFamilyDeclaration.getTypeFamilyType.getQNamesList.flatMap(_.getQNameList.map(_.getIdentifierElement))
  }

  def getIdentifierElements(derivingDeclaration: HaskellDerivingDeclaration): Seq[HaskellNamedElement] = {
    Seq(derivingDeclaration.getQName.getIdentifierElement) ++ findTypes(derivingDeclaration.getInst)
  }

  def getIdentifierElements(typeInstanceDeclaration: HaskellTypeInstanceDeclaration): Seq[HaskellNamedElement] = {
    findTypes(typeInstanceDeclaration.getExpression)
  }

  def getIdentifierElements(simpleType: HaskellSimpletype): Seq[HaskellNamedElement] = {
    findTypes(simpleType)
  }

  def getIdentifierElements(defaultDeclaration: HaskellDefaultDeclaration): Seq[HaskellNamedElement] = {
    Seq()
  }

  def getIdentifierElements(foreignDeclaration: HaskellForeignDeclaration): Seq[HaskellNamedElement] = {
    Seq()
  }

  def getIdentifierElements(moduleDeclaration: HaskellModuleDeclaration): Seq[HaskellNamedElement] = {
    Seq(moduleDeclaration.getModid)
  }

  def getModuleName(importDeclaration: HaskellImportDeclaration): Option[String] = {
    Option(importDeclaration.getModid).map(_.getName)
  }

  def getModuleName(declarationElement: HaskellDeclarationElement): Option[String] = {
    Option(declarationElement.getPresentation).map(_.getLocationString)
  }

  def getDataTypeConstructor(dataConstructorDeclaration: HaskellDataConstructorDeclarationElement): HaskellNamedElement = {
    dataConstructorDeclaration.getIdentifierElements.head
  }

  private def removeFileExtension(name: String) = {
    val fileExtension = "." + HaskellFileType.INSTANCE.getDefaultExtension
    if (name.endsWith(fileExtension)) {
      name.replaceFirst(fileExtension, "")
    } else {
      name
    }
  }

  private def findTypes(element: PsiElement) = {
    val elements = PsiTreeUtil.findChildrenOfAnyType(element, Seq(classOf[HaskellConid], classOf[HaskellConsym], classOf[HaskellTtype]): _*).toSeq.distinct

    elements.flatMap {
      case e: HaskellTtype => e.getQNameList.map(_.getIdentifierElement)
      case e: HaskellNamedElement => Seq(e)
      case _ => Seq()
    }
  }
}
