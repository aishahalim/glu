/*
 * Copyright (c) 2011 Yan Pujante
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.linkedin.glu.console.provisioner.services.storage

import org.linkedin.glu.orchestration.engine.system.SystemStorage
import org.linkedin.glu.provisioner.core.model.SystemModel
import org.linkedin.glu.orchestration.engine.fabric.Fabric
import org.linkedin.glu.console.domain.DbCurrentSystem
import org.linkedin.glu.console.domain.DbSystemModel
import org.springframework.transaction.TransactionStatus
import org.linkedin.util.annotations.Initializable
import org.linkedin.glu.console.domain.LightDbSystemModel
import org.linkedin.glu.orchestration.engine.system.SystemModelDetails

/**
 * @author yan@pongasoft.com */
public class SystemStorageImpl implements SystemStorage
{
  @Initializable
  int maxSystemsResults = 10

  @Override
  SystemModel findCurrentByFabric(String fabric)
  {
    findCurrentDetailsByFabric(fabric)?.systemModel
  }

  @Override
  SystemModelDetails findCurrentDetailsByFabric(String fabric)
  {
    createDetails(DbCurrentSystem.findByFabric(fabric, [cache: false])?.systemModel)
  }

  @Override
  SystemModel findCurrentByFabric(Fabric fabric)
  {
    findCurrentByFabric(fabric.name)
  }

  @Override
  SystemModel findBySystemId(String systemId)
  {
    findDetailsBySystemId(systemId)?.systemModel
  }

  @Override
  SystemModelDetails findDetailsBySystemId(String systemId)
  {
    createDetails(DbSystemModel.findBySystemId(systemId))
  }

  void saveCurrentSystem(SystemModel systemModel)
  {
    DbSystemModel.withTransaction { TransactionStatus txStatus ->
      DbSystemModel dbSystem = DbSystemModel.findBySystemId(systemModel.id)

      if(!dbSystem)
      {
        dbSystem = new DbSystemModel(systemModel: systemModel)
        if(!dbSystem.save())
        {
          txStatus.setRollbackOnly()
          throw new SystemStorageException(dbSystem.errors)
        }
      }

      DbCurrentSystem dbc = DbCurrentSystem.findByFabric(systemModel.fabric, [cache: false])

      if(dbc)
        dbc.systemModel = dbSystem
      else
        dbc = new DbCurrentSystem(systemModel: dbSystem,
                                  fabric: systemModel.fabric)

      if(!dbc.save())
      {
        txStatus.setRollbackOnly()
        throw new SystemStorageException(dbc.errors)
      }
    }
  }

  @Override
  boolean setAsCurrentSystem(String fabric, String systemId)
  {
    boolean res = false

    DbSystemModel.withTransaction { TransactionStatus txStatus ->
      DbSystemModel dbSystem = DbSystemModel.findBySystemId(systemId)

      DbCurrentSystem dbc = DbCurrentSystem.findByFabric(fabric, [cache: false])

      if(dbc)
      {
        res = dbc.systemModel.systemId != systemId
        dbc.systemModel = dbSystem
      }
      else
      {
        dbc = new DbCurrentSystem(systemModel: dbSystem,
                                  fabric: fabric)
        res = true
      }

      if(!dbc.save())
      {
        txStatus.setRollbackOnly()
        throw new SystemStorageException(dbc.errors)
      }
    }

    return res
  }

  @Override
  int getSystemsCount(String fabric)
  {
    DbSystemModel.countByFabric(fabric)
  }


  @Override
  Map findSystems(String fabric, boolean includeDetails, params)
  {
    if(params.offset == null)
      params.offset = 0
    params.max = Math.min(params.max ? params.max.toInteger() : maxSystemsResults, maxSystemsResults)
    params.sort = params.sort ?: 'id'
    params.order = params.order ?: 'desc'

    def systems

    if(includeDetails)
      systems = DbSystemModel.findAllByFabric(fabric, params)
    else
      systems = LightDbSystemModel.findAllByFabric(fabric, params)

    [
        systems: systems.collect { createDetails(it) },
        count: getSystemsCount(fabric),
    ]
  }

  private SystemModelDetails createDetails(DbSystemModel model)
  {
    if(model == null)
      return null

    new SystemModelDetails(dateCreated: model.dateCreated,
                           fabric: model.fabric,
                           systemId: model.systemId,
                           size: model.size ?: model.content?.size(),
                           systemModel: model.systemModel)
  }

  private SystemModelDetails createDetails(LightDbSystemModel model)
  {
    if(model == null)
      return null

    new SystemModelDetails(dateCreated: model.dateCreated,
                           fabric: model.fabric,
                           systemId: model.systemId,
                           size: model.size)
  }
}