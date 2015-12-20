package com.dimafeng.examples.scala_spring.service

import com.dimafeng.examples.scala_spring.controller.Controller._
import com.dimafeng.examples.scala_spring.dao.{BlogPostRepository, CategoryRepository, UserRepository}
import com.dimafeng.examples.scala_spring.model.{Model, BlogPost, Category, User}
import com.dimafeng.examples.scala_spring.service.Link._
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import scala.collection.JavaConverters._

@Service
class UserService @Autowired()(userRepository: UserRepository) {

  def findAll(): Seq[User] = userRepository.findAll().asScala.toSeq

  def findById = userRepository.findOne _

  def add(user: User) = userRepository.save(User(user.name))
}

@Service
class BlogPostService @Autowired()(blogPostRepository: BlogPostRepository) {

  def blogPostsByCategoryId(categoryId: String): Seq[Link] =
    blogPostRepository.findByCategoryId(categoryId).map(createLink)

  def findById(blogId: String): BlogPost = blogPostRepository.findOne(blogId)

  def linksToAllBlogPosts(): Seq[Link] = blogPostRepository.findAll()
    .asScala
    .toSeq
    .map(createLink)

  def add(blogPost: BlogPost) =
    blogPostRepository.save(BlogPost(blogPost.name, blogPost.body, blogPost.categoryIds))

  def addCategoryToBlog(blogPostId: String, categoryIds: Array[String]): Unit =
    blogPostRepository.addCategoryToBlogPostId(blogPostId, categoryIds)
}

@Service
class CategoryService @Autowired()(categoryRepository: CategoryRepository) {

  def findAll(): Seq[Category] = categoryRepository.findAll().asScala.toSeq

  def linksToCategoriesByIds(categoryIds: Seq[String]): Seq[Link] = linksToCategories(findByIds(categoryIds))

  def linksToCategories(categories: Seq[Category]): Seq[Link] = categories.map({ c => Link(c.name, urlForEntity(c)) })

  def findByIds(ids: Seq[String]): Seq[Category] = {
    val idToCat = categoryRepository.findByIds(ids.toArray).map({ c => c.id -> c }).toMap
    ids.map({ id => idToCat(id) })
  }

  def add(category: Category) = categoryRepository.save(Category(category.name))
}

case class Link(title: String, url: String)

object Link {
  def createLink(entity: Model) = entity match {
    case b: BlogPost => Link(b.name, urlForEntity(b))
  }
}